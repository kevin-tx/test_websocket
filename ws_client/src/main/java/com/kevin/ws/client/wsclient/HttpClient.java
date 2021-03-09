package com.kevin.ws.client.wsclient;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;

/**
 * @author TX
 * @date 2020/9/5 11:01
 */
public class HttpClient {
    public static void main(String[] args) {
        RestTemplate restTemplateLoadBalance = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = restTemplateLoadBalance.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof MappingJackson2XmlHttpMessageConverter) {
                iterator.remove();
            }
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        restTemplateLoadBalance.setRequestFactory(requestFactory);

        ResponseEntity<String> tdResponseGetTokenEntity = restTemplateLoadBalance
                .getForEntity("http://config-server/MS-NAT-1.properties", String.class);

        System.out.println(tdResponseGetTokenEntity.getBody());
    }
}
