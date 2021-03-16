package br.com.caelum.camel.desafio;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

import com.thoughtworks.xstream.XStream;

public class RotaHttpPollingNegociacoes {
	
	// Desafio Parte 1: HTTP Polling
	public static void main(String[] args) throws Exception {
		
		final XStream xstream = new XStream();
		xstream.alias("negociacao", Negociacao.class);
		
		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s").
			      to("http4://argentumws-spring.herokuapp.com/negociacoes").
			          convertBodyTo(String.class).
			          unmarshal(new XStreamDataFormat(xstream)).
			          split(body()).
			          log("${body}").
			    end(); //só deixa explícito que é o fim da rota
//			          setHeader(Exchange.FILE_NAME, constant("negociacoes.xml")).
//			    to("file:saida");
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
	}
}
