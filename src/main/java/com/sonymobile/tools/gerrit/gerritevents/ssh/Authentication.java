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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents authentication information to an SSH server connection.
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 * @see SshConnection
 */
public class Authentication {
    private static final Logger logger = LoggerFactory.getLogger(Authentication.class);
    private File privateKeyFile;
    private byte[] privateKeyPhrase;
    private boolean isTempFile = false;
    private String username;
    private String privateKeyFilePassword;
    /**
     * Constructor.
     * @param privateKeyFile the key.
     * @param username the username.
     * @param privateKeyFilePassword password for the key file, or null if there is none.
     * @param privateKeyPhrase phrase of privatekey.
     */
    public Authentication(File privateKeyFile,
            String username,
            String privateKeyFilePassword,
            byte[] privateKeyPhrase)  {
        this.privateKeyFile = privateKeyFile;
        this.username = username;
        this.privateKeyFilePassword = privateKeyFilePassword;
        this.privateKeyPhrase = privateKeyPhrase;
    }
    /**
     *
     * @param privateKey raw key with or without password encryption
     * @param username username to use
     * @param privateKeyFilePassword password to decrypt file - can be empty or null if none exists
     */
    public Authentication(String privateKey,
            String username,
            String privateKeyFilePassword) {
        this.privateKeyFile = this.putToTempFile(privateKey);
        this.username = username;
        this.privateKeyFilePassword = privateKeyFilePassword;
    }

    /**
     * Constructor.
     * @param privateKeyFile the key.
     * @param username the username.
     * @param privateKeyFilePassword password for the key file, or null if there is none.
     */
    public Authentication(File privateKeyFile, String username, String privateKeyFilePassword) {
        this(privateKeyFile, username, privateKeyFilePassword, null);
    }

    /**
     * Constructor.
     * With null as privateKeyFilePassword.
     * @param privateKeyFile the key.
     * @param username the username.
     */
    public Authentication(File privateKeyFile, String username)  {
        this(privateKeyFile, username, null, null);
    }

    /**
     *
     * @param keyFile key file to create
     * @return file object for key
     */
    private File putToTempFile(String keyFile) {
        try {
            File temp = File.createTempFile("gerrit_sshkey", "pem");
            FileUtils.writeStringToFile(temp, keyFile, Charset.defaultCharset());
            isTempFile = true;
            return temp;
        } catch (IOException io) {
            logger.error("Error writing key to temp file", io);
        }
        return null;
    }

    /**
     * Removes private key if temp file was used.
     */
    public void deleteTempKeyFile() {
        if (isTempFile) {
            FileUtils.deleteQuietly(privateKeyFile);
        }
    }

    /**
     * The file path to the private key.
     * @return the path.
     */
    public File getPrivateKeyFile() {
        return privateKeyFile;
    }

    /**
     * The password for the private key file.
     * @return the password.
     */
    public String getPrivateKeyFilePassword() {
        return privateKeyFilePassword;
    }

    /**
     * The username to authenticate as.
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * The phrase of private key.
     * @return the phrase.
     */
    public byte[] getPrivateKeyPhrase() {
        return privateKeyPhrase;
    }
}
