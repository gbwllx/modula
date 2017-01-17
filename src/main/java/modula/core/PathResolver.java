package modula.core;

/**
 * 路径解析接口
 */
public interface PathResolver {

    /**
     * Resolve this context sensitive path to an absolute URL.
     *
     * @param ctxPath Context sensitive path, can be a relative URL
     * @return Resolved path (an absolute URL) or <code>null</code>
     */
    String resolvePath(String ctxPath);

    /**
     * Get a PathResolver rooted at this context sensitive path.
     *
     * @param ctxPath Context sensitive path, can be a relative URL
     * @return Returns a new resolver rooted at ctxPath
     */
    PathResolver getResolver(String ctxPath);

}

