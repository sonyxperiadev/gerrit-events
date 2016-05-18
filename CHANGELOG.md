# Changelog

## 2.10.0 - 2016-05-18
Added newRev to change-merged event. [pull 54](https://github.com/sonyxperiadev/gerrit-events/pull/54), [55](https://github.com/sonyxperiadev/gerrit-events/pull/55)

Bumped Apache HttpClient dependency to 4.5.2

## 2.9.5 - 2016-04-11
Support `oldValue` instead of `updated` property for Approvals that was added in 2.9.3. [pull 51](https://github.com/sonyxperiadev/gerrit-events/pull/51), [52](https://github.com/sonyxperiadev/gerrit-events/pull/52)

## 2.9.4 - 2016-02-29
Added methods to get a read only view of registered listeners. [pull 49](https://github.com/sonyxperiadev/gerrit-events/pull/49)

## 2.9.3 - 2015-10-07
Fixed a Java 6 compatibility issue introduced in 2.9.2

Added the coming `updated` attribute to Approval. [pull 48](https://github.com/sonyxperiadev/gerrit-events/pull/48)

Added ```toString()``` methods to ```RefReplicated``` and ```RefReplicationDone``` to help in debugging [pull 47](https://github.com/sonyxperiadev/gerrit-events/pull/47)

## 2.9.2 - 2015-09-14
[JENKINS-12690](https://issues.jenkins-ci.org/browse/JENKINS-12690) Fixed SSH Key passphrase validation [pull 45](https://github.com/sonyxperiadev/gerrit-events/pull/45)

## 2.9.1 - 2015-09-03
Backwards compat for RefUpdated in Gerrit 2.12 [pull 44](https://github.com/sonyxperiadev/gerrit-events/pull/44)

## 2.9.0 - 2015-08-28
Added reviewer-added event [pull 41](https://github.com/sonyxperiadev/gerrit-events/pull/41)

Added merge-failed event [pull 42](https://github.com/sonyxperiadev/gerrit-events/pull/42)

## 2.8.0 - 2015-08-27
Added topic-changed event [pull 39](https://github.com/sonyxperiadev/gerrit-events/pull/39)

Switched from using deprecated ```DefaultHttpClient``` to ```HttpClientBuilder```  [pull 37](https://github.com/sonyxperiadev/gerrit-events/pull/37)

## 2.7.1 - 2015-05-18
Use public key as preferred authentication [pull 36](https://github.com/sonyxperiadev/gerrit-events/pull/36)

## 2.7.0 - 2015-05-04
Added patchset-notified event [pull 33](https://github.com/sonyxperiadev/gerrit-events/pull/33)

Added project-created event [pull 34](https://github.com/sonyxperiadev/gerrit-events/pull/34)

Corrected spelling of ```GerritJsonEventFactory.isInteresgingAndUsable``` [pull 35](https://github.com/sonyxperiadev/gerrit-events/pull/35)

## 2.6.1 - 2015-02-16
Updated com.jcraft.jsch to 0.1.51 [pull 32](https://github.com/sonyxperiadev/gerrit-events/pull/32)

## 2.6.0 - 2015-02-06
Allow subclasses of [GerritHandler](https://github.com/sonyxperiadev/gerrit-events/blob/master/src/main/java/com/sonymobile/tools/gerrit/gerritevents/GerritHandler.java) 
to control the creation of [EventThreads](https://github.com/sonyxperiadev/gerrit-events/blob/master/src/main/java/com/sonymobile/tools/gerrit/gerritevents/workers/EventThread.java). 
[pull 31](https://github.com/sonyxperiadev/gerrit-events/pull/31)

## 2.5.0 - 2014-12-15
Fix corrupted chars while reading from utf-8 stream [pull 28](https://github.com/sonyxperiadev/gerrit-events/pull/28)

New eventCreatedOn attribute for GerritTriggeredEvent [pull 29](https://github.com/sonyxperiadev/gerrit-events/pull/29)

## 2.4.2 - 2014-10-26
Correct the construction of the refname [pull 27](https://github.com/sonyxperiadev/gerrit-events/pull/27)

## 2.4.1 - 2014-09-16
DraftPublished as RepositoryModifiedEvent is back _(fixing the binary compatibility problem in 2.4.0)_ [pull 26](https://github.com/sonyxperiadev/gerrit-events/pull/26)

## 2.4.0 - 2014-09-09
Change-Id is now part of event attribute comparison [pull 23](https://github.com/sonyxperiadev/gerrit-events/pull/23)

Added [Callable](http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/Callable.html) versions of the send command jobs [pull 24](https://github.com/sonyxperiadev/gerrit-events/pull/24)

Fixed waiting for replication for DraftPublished and ChangeMerged events [pull 25](https://github.com/sonyxperiadev/gerrit-events/pull/25)

**WARNING:** this breaks binary compatibility with previous versions as it removed an interface implementation from DraftPublished,
and therefore should have bumped the major version. But it was an oversight in the original implementation and is more considered a bug-fix;
so only the minor version is bumped.

## 2.3.0 - 2014-08-27
Added createdOn and lastUpdated for Change and createdOn for PatchSet attributes [a154e14](https://github.com/sonyxperiadev/gerrit-events/commit/a154e14938f2982e4240e43f873d2c029e163a3e)

Added methods to AbstractSendCommandJob that throws exception on connection error for easier retrying
[pull 22](https://github.com/sonyxperiadev/gerrit-events/pull/22)

## 2.2.0 - 2014-08-12
Added CommentAdded::comment event attribute [pull 18](https://github.com/sonyxperiadev/gerrit-events/pull/18)

Expose exit code and stderr of ssh commands [pull 19](https://github.com/sonyxperiadev/gerrit-events/pull/19)

## 2.1.0 - 2014-06-16
Added ChangeKind to PatchSet _(introduced in Gerrit 2.10)_ [pull 17](https://github.com/sonyxperiadev/gerrit-events/pull/17)

## 2.0.1 - 2014-05-16
Added notify field to REST ReviewInput [pull 16](https://github.com/sonyxperiadev/gerrit-events/pull/16)

## 2.0.0 - 2014-04-25
Initial separated release. [a29ec0b](https://github.com/sonyxperiadev/gerrit-events/commit/a29ec0b1f54b040ba2bd265c6f5269380f812034)
