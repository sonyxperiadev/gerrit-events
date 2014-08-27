# Changelog

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
