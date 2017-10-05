/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 CloudBees Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.utility.steps.fs;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;


/**
 * Compute the SHA1 of a file.
 *
 * @author Emanuele Zattin &lt;emanuelez@gmail.com&gt;.
 */
public class FileSha1Step extends AbstractStepImpl {
    private final String file;

    @DataBoundConstructor
    public FileSha1Step(String file) throws Descriptor.FormException {
        if (StringUtils.isBlank(file)) {
            throw new Descriptor.FormException("can't be blank", "file");
        }
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(ExecutionImpl.class);
        }

        @Override
        public String getFunctionName() {
            return "sha1";
        }

        @Override
        public String getDisplayName() {
            return "Compute the SHA1 of a given file";
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckFile(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Needs a value");
            } else {
                return FormValidation.ok();
            }
        }
    }

    public static class ExecutionImpl extends AbstractSynchronousNonBlockingStepExecution<String> {
        private static final long serialVersionUID = 1L;

        @StepContextParameter
        private transient FilePath ws;

        @Inject
        private transient FileSha1Step step;

        @Override
        protected String run() throws Exception {
            FilePath filePath = ws.child(step.getFile());
            return filePath.act(new ComputeSha1());
        }

        private class ComputeSha1 implements FilePath.FileCallable<String> {
            @Override
            public String invoke(File file, VirtualChannel virtualChannel) throws IOException, InterruptedException {
                HashFunction hf = Hashing.sha1();
                HashCode hc = hf.newHasher().putBytes(Files.toByteArray(file)).hash();
                return hc.toString();
            }

            @Override
            public void checkRoles(RoleChecker roleChecker) throws SecurityException {
            }
        }
    }

}
