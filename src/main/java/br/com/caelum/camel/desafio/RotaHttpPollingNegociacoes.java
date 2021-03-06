package br.com.caelum.camel.desafio;

import java.text.SimpleDateFormat;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.thoughtworks.xstream.XStream;

public class RotaHttpPollingNegociacoes {
	
	// Desafio Parte 1: HTTP Polling
	public static void main(String[] args) throws Exception {
		
		SimpleRegistry registro = new SimpleRegistry();
		registro.put("mysql", criaDataSource());
		
		final XStream xstream = new XStream();
		xstream.alias("negociacao", Negociacao.class);
		
		CamelContext context = new DefaultCamelContext(registro);
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s").
			      to("http4://argentumws-spring.herokuapp.com/negociacoes").
			          convertBodyTo(String.class).
			          unmarshal(new XStreamDataFormat(xstream)).
			          split(body()).
			          process(new Processor() {
						
						@Override
						public void process(Exchange exchange) throws Exception {
							
							Negociacao negociacao = exchange.getIn().getBody(Negociacao.class);
				            exchange.setProperty("preco", negociacao.getPreco());
				            exchange.setProperty("quantidade", negociacao.getQuantidade());
				            String data = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(negociacao.getData().getTime());
				            exchange.setProperty("data", data);
						}
					}).
			        setBody(simple("insert into negociacao(preco, quantidade, data) values (${property.preco}, ${property.quantidade}, '${property.data}')")).
			        log("${body}"). //logando o comando sql
			        delay(1000). //esperando 1s para deixar a execu????o mais f??cil de entender
			    to("jdbc:mysql"); //usando o componente jdbc que envia o SQL para mysql
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
	}
	
	private static MysqlConnectionPoolDataSource criaDataSource() {
		
	    MysqlConnectionPoolDataSource mysqlDs = new MysqlConnectionPoolDataSource();
	    mysqlDs.setDatabaseName("camel");
	    mysqlDs.setServerName("172.18.0.2");
	    mysqlDs.setPort(3306);
	    mysqlDs.setUser("root");
	    mysqlDs.setPassword("root");
	    return mysqlDs;
	}
}
