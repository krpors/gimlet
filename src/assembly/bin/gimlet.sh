#!/usr/bin/env bash

java -cp "../lib/*:../drivers/*" --module-path="../lib" --add-modules=javafx.controls cruft.wtf.gimlet.GimletApp $@