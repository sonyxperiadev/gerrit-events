/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2014 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.tools.gerrit.gerritevents.ssh;

import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Base util for connecting, authenticating and doing stuff on an ssh client->server connection.
 * @author Robert Sandell robert.sandell@sonyericsson.com
 */
public final class SshUtil {

    /**
     * Default hidden constructor.
     */
    private SshUtil() {
    }

    /**
     * Does a series of checks in the file to see if it is a valid private key file.
     * @param keyFile the file
     * @return true if valid.
     */
    public static boolean isPrivateKeyFileValid(File keyFile) {
        return parsePrivateKeyFile(keyFile) != null;
    }

    /**
     * Parses the keyFile hiding any Exceptions that might occur.
     * @param keyFile the file.
     * @return the "parsed" file.
     */
    private static SshPrivateKeyFile parsePrivateKeyFile(File keyFile) {
        try {
            byte[] data = FileUtils.readFileToByteArray(keyFile);
            SshPrivateKeyFile key =  SshPrivateKeyFileFactory.parse(data);
            return key;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Checks to see if the passPhrase is valid for the private key file.
     * @param keyFilePath the private key file.
     * @param passPhrase the password for the file.
     * @return true if it is valid.
     */
    public static boolean checkPassPhrase(File keyFilePath, String passPhrase) {

        try {
            SshPrivateKeyFile key =  parsePrivateKeyFile(keyFilePath);
            boolean isValidPhrase = passPhrase != null && !passPhrase.trim().isEmpty();
            if (key == null) {
                return false;
            } else if (key.isPassphraseProtected() != isValidPhrase) {
                return false;
            } else if (key.isPassphraseProtected()) {
                // Throws an exception if passphrase is invalid
                key.toKeyPair(passPhrase);
                return true;
            }
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
