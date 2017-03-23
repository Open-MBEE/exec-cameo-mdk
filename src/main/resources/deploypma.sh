# script will deploy a zipped MD package to /opt/local/<mdk version> and link the directory to /opt/local/MD
# zip must be manually retrieved from artifactory via wget
# will also place the docweb.sh script in the appropriate location. script should be in the same location as the md zip.

#echo $1 zipped file / directory name
#echo $2 user directory where the zip is located

cd /opt/local
mkdir $1
cd $1
cp $2/$1.zip .
unzip -qq $1.zip
touch $1.ver
rm $1.zip
cd bin
chmod +x magicdraw
cd ..
mkdir automations
cd automations
cp $2/docweb.sh .
cd ..
cd ..
rm -r MD
ln -s $1 MD
chown -R jenkins:jenkins $1
chown -R jenkins:jenkins MD
