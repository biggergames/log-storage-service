package com.biggergames.backend.logstorageservice.config.time;

import io.micrometer.common.annotation.NoOpValueResolver;
import io.micrometer.common.annotation.ValueExpressionResolver;
import io.micrometer.common.annotation.ValueResolver;
import io.micrometer.core.aop.MeterTagAnnotationHandler;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

@Configuration
public class TimeAspectConfig {
    private static final String BG_ENV = "bg_env";
    private static final String APPLICATION = "application";
    @Value("${spring.profiles.active}")
    private String[] activeProfiles;
    @Value("${info.app.name}")
    private String appName;

    /**
     * Added due to the warning <i>"Micrometer’s Spring Boot configuration does not recognize @Timed on arbitrary
     * methods."</i> to enable Timed annotation in methods services such as AccountService.
     * <br/>
     * I have doubts about a potential (negligible?) overhead (tuna, 20220216)
     *
     * @see <a href="https://micrometer.io/docs/concepts#_the_timed_annotation">Micrometer Timed Annotation</a>
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        TimedAspect ta = new TimedAspect(registry);
        ValueResolver valueResolver = new NoOpValueResolver();
        ValueExpressionResolver valueExpressionResolver = new SpelValueExpressionResolver();
        ta.setMeterTagAnnotationHandler(new MeterTagAnnotationHandler(aClass -> valueResolver, aClass -> valueExpressionResolver));
        return ta;
    }

    private Tags tags() {
        return Tags.of(springProfileAsTag(), applicationNameAsTag());
    }

    public Tag springProfileAsTag() {
        if (!ObjectUtils.isEmpty(activeProfiles)) {
            return Tag.of(BG_ENV, activeProfiles[0]);
        } else {
            return Tag.of(BG_ENV, "unknown");
        }
    }

    public Tag applicationNameAsTag() {
        return Tag.of(APPLICATION, appName);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(tags());
    }
}
