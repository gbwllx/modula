package modula.core.model;

import java.util.Comparator;

/**
 * {@link EnterableState} and {@link Transition}继承这个接口
 * 排序规则：
 * 1. 祖先state在后继state前面
 * 2. state中的transitions在后继state前面
 * 确保定义中少于 Integer.MAX_VALUE 个元素
 */
public interface DocumentOrder {

    Comparator<DocumentOrder> documentOrderComparator = new Comparator<DocumentOrder>() {
        @Override
        public int compare(final DocumentOrder o1, final DocumentOrder o2) {
            return o1.getOrder() - o2.getOrder();
        }
    };

    Comparator<DocumentOrder> reverseDocumentOrderComparator = new Comparator<DocumentOrder>() {
        @Override
        public int compare(final DocumentOrder o1, final DocumentOrder o2) {
            return o2.getOrder() - o1.getOrder();
        }
    };

    /**
     * @return the relative document order within the Modula document of this element
     */
    int getOrder();
}
