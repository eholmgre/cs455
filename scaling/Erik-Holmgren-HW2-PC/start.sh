CLASSES=build/classes/java/main
SCRIPT="cd $(pwd)/$CLASSES;
java -cp . cs455.scaling.client.Client $(hostname) 6666 5"
#$1 is the command-line argument specifying how many times it should open the machine list. 
#If 2 is specified, and there are 10 machines on the list, this will open and run on 20 machines.

xfce4-terminal --working-directory=$(pwd)/$CLASSES -e "java -cp . cs455.scaling.server.Server 6666 10 50 5"

for ((j=0;j<$1;j++))
do
	COMMAND='xfce4-terminal'
	for i in `cat machine_list`
	do
		echo 'logging into '$i
		OPTION='--tab -e "ssh -t '$i' '$SCRIPT'"'
		COMMAND+=" $OPTION"
		done
		eval $COMMAND &
	done
