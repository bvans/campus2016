#!/usr/bin/env bash
awk '{print $1}' a.log |sort|uniq -c|sort -nr|head -n 10