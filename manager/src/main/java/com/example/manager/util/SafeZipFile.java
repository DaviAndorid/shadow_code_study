

package com.example.manager.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 避免ZipperDown漏洞
 */
public class SafeZipFile extends ZipFile {

    public SafeZipFile(File file) throws IOException {
        super(file);
    }

    @Override
    public Enumeration<? extends ZipEntry> entries() {
        return new SafeZipEntryIterator(super.entries());
    }

    private static class SafeZipEntryIterator implements Enumeration<ZipEntry> {

        final private Enumeration<? extends ZipEntry> delegate;

        private SafeZipEntryIterator(Enumeration<? extends ZipEntry> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasMoreElements() {
            return delegate.hasMoreElements();
        }

        @Override
        public ZipEntry nextElement() {
            ZipEntry entry = delegate.nextElement();
            if (null != entry) {
                String name = entry.getName();
                if (null != name && (name.contains("../") || name.contains("..\\"))) {
                    throw new SecurityException("非法entry路径:" + entry.getName());
                }
            }
            return entry;
        }
    }
}
