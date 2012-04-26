package org.agilewiki.jactor;

import org.agilewiki.jactor.concurrent.JAThreadManager;
import org.agilewiki.jactor.concurrent.ThreadManager;
import org.agilewiki.jactor.lpc.JLPCMailbox;

/**
 * <p>
 * Implements MailboxFactory.
 * In general you need only one instance of MailboxFactory per program.
 * </p>
 * <pre>
 *         MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
 *         try {
 *             ...
 *         } finally {
 *             mailboxFactory.close();
 *         }
 * </pre>
 */
final public class JAMailboxFactory implements MailboxFactory {

    /**
     * The thread manager.
     */
    private ThreadManager threadManager;

    private int mailboxSize;

    public JAMailboxFactory(ThreadManager threadManager, int mailboxSize) {
        this.threadManager = threadManager;
        this.mailboxSize = mailboxSize;
    }

    /**
     * Create a JAMailboxFactory
     *
     * @param threadCount The number of concurrent to be used.
     * @return A new JAMailboxFactory.
     */
    public static JAMailboxFactory newMailboxFactory(int threadCount) {
        return new JAMailboxFactory(JAThreadManager.newThreadManager(threadCount), 0);
    }

    /**
     * Create a JAMailboxFactory
     *
     * @param threadCount The number of concurrent to be used.
     * @return A new JAMailboxFactory.
     */
    public static JAMailboxFactory newMailboxFactory(int threadCount, int mailboxSize) {
        return new JAMailboxFactory(JAThreadManager.newThreadManager(threadCount), mailboxSize);
    }

    /**
     * Returns the thread manager.
     *
     * @return The thread manager.
     */
    public ThreadManager getThreadManager() {
        return threadManager;
    }

    /**
     * Stop all the threads as they complete their tasks.
     */
    @Override
    public void close() {
        threadManager.close();
    }

    @Override
    public int getMailboxSize() {
        return mailboxSize;
    }

    /**
     * Create a mailbox with default size
     *
     * @return A new mailbox.
     */
    @Override
    final public Mailbox createMailbox() {
        final JLPCMailbox mailbox = new JLPCMailbox(this);
        return mailbox;
    }

    /**
     * Create a mailbox with default size
     *
     * @return A new mailbox.
     */
    @Override
    final public Mailbox createMailbox(int maxSize) {
        final JLPCMailbox mailbox = new JLPCMailbox(this, false, maxSize);
        return mailbox;
    }

    /**
     * Create an asynchronous mailbox.
     *
     * @return A new asynchronous mailbox.
     */
    @Override
    final public Mailbox createAsyncMailbox() {
        final JLPCMailbox mailbox = new JLPCMailbox(this, true, 0);
        return mailbox;
    }

    /**
     * Create an asynchronous mailbox.
     *
     * @return A new asynchronous mailbox.
     */
    @Override
    final public Mailbox createAsyncMailbox(int maxSize) {
        final JLPCMailbox mailbox = new JLPCMailbox(this, true, maxSize);
        return mailbox;
    }
}
