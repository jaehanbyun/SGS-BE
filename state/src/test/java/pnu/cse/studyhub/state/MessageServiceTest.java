package pnu.cse.studyhub.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pnu.cse.studyhub.state.config.TCPAuthClientGateway;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;
import pnu.cse.studyhub.state.service.MessageService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class MessageServiceTest {

    @Mock
    private TCPAuthClientGateway tcpAuthClientGateway;

    @InjectMocks
    private MessageService messageService;

    private List<RealTimeData> batch = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create some test data
        RealTimeData rtData1 = new RealTimeData();
        RealTimeData rtData2 = new RealTimeData();

        rtData1.setUserId("user1");
        rtData1.setStudyTime("02:00:00");
        rtData2.setUserId("user2");
        rtData2.setStudyTime("03:00:00");

        batch.add(rtData1);
        batch.add(rtData2);

    }

    @Test
    public void testProcessAndSendBatch() {
        // Call the method under test
        messageService.processAndSendBatch(batch);

        // Verify that the send method was called once
        verify(tcpAuthClientGateway, times(1)).send(anyString());
    }
}

