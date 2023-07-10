#!/bin/bash

brew update
brew install zip
brew install unzip
brew install maven
brew install graphviz

curl -s "https://get.sdkman.io" | bash

bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && yes | sdk install java 11.0.17-librca"
bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && yes | sdk install java 8.0.352-librca"