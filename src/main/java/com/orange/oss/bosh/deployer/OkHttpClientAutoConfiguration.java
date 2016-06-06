package com.orange.oss.bosh.deployer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orange.oss.bosh.deployerfeigncfg.FeignConfiguration;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;



@Configuration
@ConditionalOnClass(com.squareup.okhttp.OkHttpClient.class)
public class OkHttpClientAutoConfiguration {
 
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(FeignConfiguration.class.getName());
	
	
	@Value("${director.proxyHost}")
	private String proxyHost;
	
	@Value("${director.proxyPort}")	
	private int proxyPort;
	
 
    @Bean
    public OkHttpClient squareHttpClient() {
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		TrustManager[] trustAllCerts = new TrustManager[] { new TrustAllCerts() };

		SSLSocketFactory sslSocketFactory=null;
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			sslSocketFactory = (SSLSocketFactory) sc.getSocketFactory();
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			new IllegalArgumentException(e);
		}

		logger.info("===> configuring OkHttp");
		com.squareup.okhttp.OkHttpClient ohc=new com.squareup.okhttp.OkHttpClient();
		ohc.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
		//ohc.setFollowRedirects(false);
		ohc.setHostnameVerifier(hostnameVerifier);
		ohc.setSslSocketFactory(sslSocketFactory);
		if ((this.proxyHost!=null )&&(this.proxyHost.length()>0)){
		Proxy proxy=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost, this.proxyPort));
		ohc.setProxy(proxy);
        ohc.setProxySelector(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
            return Arrays.asList(proxy);
            }
			@Override
			public void connectFailed(URI uri, SocketAddress socket, IOException e) {
				throw new IllegalArgumentException("connect to proxy failed",e);
			}
        });}
		
	 return ohc;
    }    	
    
    
	  public static class TrustAllCerts extends X509ExtendedTrustManager {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
			}
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

		   }
    
}