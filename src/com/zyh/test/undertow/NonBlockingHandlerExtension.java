package com.zyh.test.undertow;

import java.util.concurrent.TimeUnit;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.util.Headers;

import javax.servlet.ServletContext;

public class NonBlockingHandlerExtension implements ServletExtension {
	
	public static void main(String[] argv){
		Undertow server = Undertow.builder()
		        .addHttpListener(8080, "localhost")
		        .setHandler(exchange->{
		        	if(exchange.isInIoThread()){
		        		exchange.dispatch(()->{
		        			try {
								TimeUnit.SECONDS.sleep(10);
							} catch (Exception e) {
								e.printStackTrace();
							}
		                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		                    exchange.getResponseSender().send("Hello World");
		        		});
		        	}
		        })
		        .build();
		server.start();
	}

	@Override
	public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.addInitialHandlerChainWrapper(handler-> {
                return Handlers.path()
                        .addPrefixPath("/", handler)
                        .addPrefixPath("/hello",exchange->{
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                            exchange.getResponseSender().send("Hello World");
                        });
        });		
	}
	
	
	
}