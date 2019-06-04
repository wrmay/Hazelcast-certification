#!/bin/bash
envname=$1
group=$2
command=$3
ansible --inventory=$envname/inventory.ini --key=$envname/$envname.pem --user=ec2-user $group -a "$command"
