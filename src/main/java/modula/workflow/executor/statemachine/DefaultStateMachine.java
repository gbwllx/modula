package modula.workflow.executor.statemachine;


import modula.core.model.Modula;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */
public class DefaultStateMachine extends AbstractStateMachine{
    public DefaultStateMachine(Modula modula){
        super(modula);
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-workflow.xml");

    }
}
