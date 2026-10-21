package br.com.netwatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class StoreTest {
    @TempDir Path temp;
    private String previousHome;
    private Store store;
    private Store open(){previousHome=System.getProperty("user.home");System.setProperty("user.home",temp.toString());store=new Store();store.initialize();return store;}
    @AfterEach void cleanup(){if(store!=null)store.close();if(previousHome!=null)System.setProperty("user.home",previousHome);}
    @Test void recordsOnlyStateTransitionsAndExportsPortugueseText(){Store s=open();long id=s.switches().get(0).id();long before=s.eventCount();s.transition(id,"UP");assertEquals(before,s.eventCount());s.transition(id,"DOWN");assertEquals(before+1,s.eventCount());String report=s.report(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));assertTrue(report.contains("RELATÓRIO DE MONITORAMENTO"));assertTrue(report.contains("FIM DO RELATÓRIO"));}
    @Test void clearingEventsLeavesAuditEntry(){Store s=open();assertTrue(s.eventCount()>0);s.clearLogs();assertEquals(0,s.eventCount());assertTrue(s.auditCount()>0);}
}
