package stateMachine.state;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/23.
 */
public class StateMachineMap {

    private Map<State,Map<Event, SopExec>> tableMap;

    public StateMachineMap() {
        tableMap = new HashMap<>();
    }

    public void put(State state, Event event, SopExec sopExec) {
        Map<Event, SopExec> eventSopExecMap = tableMap.get(state);
        if (eventSopExecMap == null) {
            Map<Event, SopExec> execMap = new HashMap<>();
            execMap.put(event, sopExec);
            tableMap.put(state, execMap);
            return;
        }
        eventSopExecMap.put(event, sopExec);
    }

    public SopExec get(State state, Event event) {
        return tableMap.get(state).get(event);
    }

}
