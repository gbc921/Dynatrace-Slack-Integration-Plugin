# SlackChat
SlackChat integration for Dynatrace AppMon
This integration works via WebHooks for Slack and was based on [HipChat](https://github.com/dynaTrace/Dynatrace-HipChat-Plugin).
Find further information about the SlackPlugin in the [Dynatrace community](https://community.dynatrace.com/community/display/DL/Slack+Integration+Plugin)

The main features are:
- The Channel that messages will be posted can be specified in every alert;
- Printing Alert message can be disabled;
- Printing Alert description can be disabled;
- There is a check to verify whether alert was fired from DB Monitor Plugin to avoid alert overload. The DB plugin fires one individual alert for each host plus a general alert. The check only alerts the general alert (there should be no problem leaving it on all the time, if you find any problem please file a bug report).

Tested with Dynatrace 6.5, but other versions should work too (if not try compiling it manually).
