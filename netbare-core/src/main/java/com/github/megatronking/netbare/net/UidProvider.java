package com.github.megatronking.netbare.net;

/**
 * This interface provides a known uid for a session.
 *
 * @author Megatron King
 * @since 2019/1/27 21:31
 */
public interface UidProvider {

    int UID_UNKNOWN = -1;

    /**
     * Returns a known uid for this session, if the uid is unknown should return {@link #UID_UNKNOWN}.
     *
     * @param session Network session.
     * @return A known uid or {@link #UID_UNKNOWN}.
     */
    int uid(Session session);

}
