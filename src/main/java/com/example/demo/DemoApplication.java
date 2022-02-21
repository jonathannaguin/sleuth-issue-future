package com.example.demo;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class DemoApplication
{
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Tracer tracer;

    public static void main(String[] args)
    {
        SpringApplication.run(DemoApplication.class, args);
    }

    @EventListener({ ApplicationReadyEvent.class })
    public void start()
    {
        logger.info("Starting...");

        Span pollSpan = tracer.newTrace()
                              .name("poll");

        // if you remove the top SpanInScope, then the line for the response will not have any tracing
        try (Tracer.SpanInScope span = tracer.withSpanInScope(pollSpan.start()))
        {
            Flux<Message> flux = Flux.fromIterable(List.of(receiveMessages()));

            flux.flatMap(message -> {
                    var mono = deserialize(message, MyObjectWithTracing.class);
                    var traceId = message.getMessageAttributes()
                                         .get("traceId");
                    var spanId = message.getMessageAttributes()
                                        .get("spanId");

                    var newContext = TraceContext.newBuilder()
                                                 .traceId(Long.parseLong(traceId))
                                                 .spanId(Long.parseLong(spanId))
                                                 .build();

                    return mono.flatMap(myObject -> {
                        // process object
                        try (Tracer.SpanInScope ws = tracer.withSpanInScope(tracer.toSpan(newContext)
                                                                                  .start()))
                        {
                            logger.info("Do something {}", myObject); // correct tracing information in the logs, fb134c9c33e7225a
                            return Mono.defer(() -> {
                                           // create a Mono from Future, similar to the AWS SDK async invocation
                                           return Mono.fromFuture(() -> {
                                               // simple return the object in a CompletableFuture
                                               return CompletableFuture.completedFuture(myObject);
                                           });
                                       })
                                       .map(o -> {
                                           logger.info("Response {}", o); // correct tracing information in the logs, should be fb134c9c33e7225a
                                           return o;
                                       });
                        }
                    });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        }
    }

    private Mono<?> deserialize(Message source,
                                Class<?> target)
    {
        try
        {
            return Mono.just(objectMapper.readValue(source.getBody(), target));
        }
        catch (JsonProcessingException e)
        {
            return Mono.error(e);
        }
    }

    private Message receiveMessages()
    {

        var id = -354855711862742438L; //fb134c9c33e7225a
        var message = new Message();
        message.setBody(String.format("{\"traceId\":\"%s\", \"spanId\":\"%s\"}", id, id));
        message.getMessageAttributes()
               .put("traceId", String.valueOf(id));
        message.getMessageAttributes()
               .put("spanId", String.valueOf(id));
        return message;
    }

    @Autowired
    public void setTracer(Tracer tracer)
    {
        this.tracer = tracer;
    }
}
