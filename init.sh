#!/bin/bash
set -euo pipefail

##################################### CHECK REQUIREMENTS #####################################
required_node_version=20
java_required=21



if ! command -v node >/dev/null 2>&1
then
    echo "Node is not installed"
    exit 1
fi
current_node_version=$(node -v | sed 's/v\([0-9][0-9]*\).*/\1/')
if [ "$current_node_version" != "$required_node_version" ]
then
    echo "Node $required_node_version is required for this. You have node $current_node_version"
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1
then
    echo "Maven is not installed"
    exit 1
fi

if ! command -v java >/dev/null 2>&1
then
    echo "Java is not installed"
    exit 1
fi

java_current=$(java --version 2>/dev/null | head -n 1 | sed 's/[^0-9]*\([0-9][0-9]*\).*/\1/')

if [ "$java_current" != "$java_required" ]
then
    echo "Java $java_required is required. You have Java $java_current"
    exit 1
fi

############################################ INPUT ############################################
read -p "Application name (MyApplication) (no spaces, StartWithCapitalLetter): " appname
read -p "Group ID (com.myapplication) (only lowercase, no spaces): " groupid

echo "Initializing application $appname..."

lowerappname=$(echo "$appname" | tr '[:upper:]' '[:lower:]')
groupid_dirs=$(echo "$groupid" | tr '.' '/')

########################################## FIX FILES ##########################################
find . \( -name "*.xml" -o -name "*.csv" -o -name "*.txt" -o -name "*.tsx" -o -name "*.json" -o -name "*.yaml" -o -name "*.html" -o -name "*.java" \) -type f -print0 \
| while IFS= read -r -d '' file; do
    sed -i '' "s/appweb.appname/${appname}/g" "$file"
    sed -i '' "s/appweb.lowerappname/${lowerappname}/g" "$file"
    sed -i '' "s/appweb.groupid/${groupid}/g" "$file"
done

# TODO: fix groupid
mv backend/impl/src/main/java/com/appweb/application backend/impl/src/main/java/${groupid_dirs}
mv backend/impl/src/main/java/${groupid_dirs}/Application.java backend/impl/src/main/java/${groupid_dirs}/${appname}Application.java

####################################### SETUP FRONTEND #######################################
echo "Running npm install in frontend folder..."
pushd frontend
npm install

echo "Generating api..."
npm run generate:api
popd

####################################### SETUP BACKEND ########################################
echo "Running mvn clean install in backend folder..."
mvn clean install

############################################ DONE ############################################
echo ""
echo "###############################"
echo ""
echo "🚀Your project $appname is ready!"