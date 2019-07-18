#!/bin/bash
envname=$1
play=$2
ansible-playbook --inventory=$envname/inventory.ini --key=$envname/$envname.pem --forks=32 -e envname=$envname $play.yaml 
