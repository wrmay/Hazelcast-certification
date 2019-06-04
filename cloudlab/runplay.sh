#!/bin/bash
envname=$1
play=$2
ansible-playbook --inventory=$envname/inventory.ini --key=$envname/$envname.pem $play.yaml
