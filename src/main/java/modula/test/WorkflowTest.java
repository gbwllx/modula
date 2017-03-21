package modula.test;

import modula.engine.DefaultWorkflowEngine;
import modula.engine.event.WorkflowEvent;
import modula.engine.context.WorkflowContext;
import modula.parser.model.ModelException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/19.
 */
public class WorkflowTest {
    public static void main(String[] args) throws InterruptedException, IOException, ModelException, XMLStreamException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-workflow.xml");

        DefaultWorkflowEngine workflow = (DefaultWorkflowEngine) context.getBean("workflow");

        WorkflowContext workflowContext = new WorkflowContext("modula");
        workflowContext.setEvents(new WorkflowEvent("makeOrder"));
        workflow.execute(workflowContext);
        Thread.sleep(2000L);

        workflowContext.setEvents(new WorkflowEvent("buyerPay"));
        workflow.execute(workflowContext);
        Thread.sleep(2000L);

        workflowContext.setEvents(new WorkflowEvent("sellerSend"));
        workflow.execute(workflowContext);
        Thread.sleep(2000L);

        workflowContext.setEvents(new WorkflowEvent("buyerReceive"));
        workflow.execute(workflowContext);
        Thread.sleep(2000L);
    }
}
