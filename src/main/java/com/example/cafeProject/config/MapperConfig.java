package com.example.cafeProject.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // null값은 매핑시 스킵하도록 하는 설정
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return modelMapper;
    }
}
