#!/bin/bash

users=`cat /etc/passwd | cut -d: -f1`
users_logged=`who | cut -d' ' -f1 | sort | uniq`

for u in $users; do
  cmd="curl --data-binary @- http://admin:admin@localhost:9091/metrics/job/$u"
  if [[ "$users_logged" == *"$u"* ]]; then
    # echo "some_metric 3.14" | curl --data-binary @- http://admin:admin@localhost:9091/metrics/job/some_job
    echo "linux_user 1" | `echo $cmd`
  else
    echo "linux_user 0" | `echo $cmd`
  fi
done





