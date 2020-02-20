package com.chuang.qapp.compatible;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author fandy.lin
 */
public class BizException extends RuntimeException implements Iterable<Throwable> {
    private static final long serialVersionUID = 3002667491943962073L;
    private static final AtomicReferenceFieldUpdater<BizException, BizException> nextUpdater =
            AtomicReferenceFieldUpdater.newUpdater(BizException.class, BizException.class, "next");
    private Status status = DefaultStatus.UNKNOWN_ERROR;
    private Object result;

    private volatile BizException next;

    public BizException() {
    }

    public BizException(Status status) {
        this.status = status;
    }

    public BizException(String reason) {
        super(reason);
    }

    public BizException(Status status, String reason) {
        super(reason);
        this.status = status;
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public BizException(Status status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public BizException(Status status, String reason, Throwable cause) {
        super(reason, cause);
        this.status = status;
    }

    public BizException result(Object result) {
        this.result = result;
        return this;
    }

    public Object getResult() {
        return this.result;
    }

    @Override
    public Iterator<Throwable> iterator() {
        return new Iterator<Throwable>() {
            BizException firstException = BizException.this;
            BizException nextException = this.firstException.getNextException();
            Throwable cause = this.firstException.getCause();
            @Override
            public boolean hasNext() {
                if (this.firstException != null || this.nextException != null || this.cause != null)
                    return true;
                return false;
            }
            @Override
            public Throwable next() {
                Throwable throwable = null;
                if (this.firstException != null) {
                    throwable = this.firstException;
                    this.firstException = null;
                } else if (this.cause != null) {

                    throwable = this.cause;
                    this.cause = this.cause.getCause();
                } else if (this.nextException != null) {
                    throwable = this.nextException;
                    this.cause = this.nextException.getCause();
                    this.nextException = this.nextException.getNextException();
                } else {
                    throw new NoSuchElementException();
                }
                return throwable;
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public BizException getNextException() {
        return this.next;
    }

    public void setNextException(BizException ex) {
        BizException current = this;
        while (true) {
            BizException next = current.next;
            if (next != null) {
                current = next;
                continue;
            }
             if (nextUpdater.compareAndSet(current, null, ex)) {
                return;
            }
              current = current.next;
        }
    }

    public Status getStatus() {
        return this.status;
    }


    @Override
    public String toString() {
      String s = getClass().getName();
      String orgMsg = getLocalizedMessage();
      String statusMsg = this.status.toStr();
      String message = (orgMsg != null) ? (statusMsg + " ||| " + orgMsg) : statusMsg;
      return s + ": " + message;
    }
}

