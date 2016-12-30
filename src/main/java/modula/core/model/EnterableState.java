package modula.core.model;

import java.util.List;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public abstract class EnterableState {
    /**
     * 状态的文档排序
     */
    //private int order;

    /**
     * 可选的OnEntry集合
      */
    private List<OnEntry> onEntries;

    /**
     * 可选的OnExit集合
     */
    private List<OnExit> onExits;
}
