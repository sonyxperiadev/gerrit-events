/*
 *  The MIT License
 *
 *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
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
package com.sonymobile.tools.gerrit.gerritevents.dto.events;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PROVIDER;
import net.sf.json.JSONObject;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritJsonEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;


/**
 * A DTO representation of a Gerrit triggered Event.
 *
 * @author David Pursehouse &lt;david.pursehouse@sonyericsson.com&gt;
 */
public abstract class GerritTriggeredEvent implements GerritJsonEvent {

    private JSONObject json;

    /**
     * The account that triggered the event.
     */
    protected Account account;

    /**
     * The provider that provide the event.
     */
    protected Provider provider;

    /**
     * Time stamp when the event was received.
     */
    protected long receivedOn;

    /**
     * Standard constructor, initialize the receivedOn time stamp.
     */
    public GerritTriggeredEvent() {
        receivedOn = System.currentTimeMillis();
    }

    /**
     * The account that triggered the event.
     *
     * @return the account.
     */
    public Account getAccount() {
        return account;
    }

    /**
     * The account that triggered the event.
     *
     * @param account the account.
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * The provider that provided the event.
     *
     * @return the provider.
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * The provider that provided the event.
     *
     * @param provider the provider.
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * Time stamp when the event was received.
     * @return the receivedOn time stamp
     */
    public long getReceivedOn() {
        return receivedOn;
    }

    /**
     * Time stamp when the event was received.
     * @param receivedOn the receivedOn to set
     */
    public void setReceivedOn(long receivedOn) {
        this.receivedOn = receivedOn;
    }

    @Override
    public void fromJson(JSONObject rawJson) {
        this.json = rawJson;
        if (json.containsKey(PROVIDER)) {
            provider = new Provider(json.getJSONObject(PROVIDER));
        }
    }

    @Override
    public JSONObject getJson() {
        return this.json;
    }

}
