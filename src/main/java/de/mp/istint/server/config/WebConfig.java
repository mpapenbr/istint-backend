package de.mp.istint.server.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@Profile({ "dev", "prod" })
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware {

    private final long MAX_AGE_SECS = 3600;
    private ApplicationContext applicationContext;

    /**
     * Note: The CORS-confgiruation via this method is essential. This setting is handled by Spring
     * MVC which is called prior to Spring Security CORS.
     * see https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#cors
     * 
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        /*
         * Die Idee ist hier:
         * Der ObjectMapper wird via application.yml konfiguriert.
         * Wir wollen die Standard-HttpMesssageConverter behalten, aber denjeningen, der die
         * JSON-Konvertierung macht, etwas modifizieren.
         * Leider müssen wir dafür per instanceof suchen und dann dort für die Treffer unseren
         * passenden
         * ObjectMapper eintragen.
         *
         * Hintergrund: Der bereits eingetragen ObjectMapper ist ziemlich nackt.
         * Den müssen/wollen wir hier etwas aufhübschen.
         *
         * Anlass war, dass GraphQL-Queries, die Zeilenumbrüche in Strings hatten, nicht verarbeitet
         * werden
         * konnten. Dafür kann man den ObjectMapper per ALLOW_UNQUOTED_CONTROL_CHARS konfigurieren.
         * Dies steht jetzt in der application.yml.
         *
         */
        Jackson2ObjectMapperBuilder builder = applicationContext.getBean(Jackson2ObjectMapperBuilder.class);
        ObjectMapper om = builder.build();

        converters.stream()
                .filter(item -> item instanceof MappingJackson2HttpMessageConverter)
                .map(item -> (MappingJackson2HttpMessageConverter) item)
                .forEach(item -> {
                    item.setObjectMapper(om);
                });
        //        converters.add(new MappingJackson2XmlHttpMessageConverter(builder.createXmlMapper(true).build()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
