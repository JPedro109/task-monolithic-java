package com.jpmns.task.integration.common.abstracts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jpmns.task.TaskApplication;
import com.jpmns.task.integration.common.container.PostgresContainerConfig;

@SpringBootTest(classes = TaskApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(PostgresContainerConfig.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;
}
