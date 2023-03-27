# StoreMonitoring
A spring-boot application in which user can generate reports regarding store uptime and downtime, according to data provided.

## How to run?
* Clone this repository.
* Run MySql server in your machine.
* Provide following environment variables.
  * _db_url_
  * _db_username_
  * _db_password_
* Go to root directory of this project using terminal.
* Run command:- `./mvnw spring-boot:run`
* Application will start on port 8081.


## How to use?

#### Endpoints exposed:
###### 1. /import/store-hours
* Used to upload and import store hours in a CSV format.
* POST request.
* Accepts only CSV format file with key "file".
* Adds records to database.


###### 2. /import/store-status
* Used to upload and import store status in a CSV format.
* POST request.
* Accepts only CSV format file with key "file".
* Adds records to database.


###### 3. /import/store-timezone
* Used to upload and import store local timezones in a CSV format.
* POST request.
* Accepts only CSV format file with key "file".
* Adds records to database.


###### 4. /trigger_report
* Used to trigger report generation using provided CSV data.
* POST request.
* Returns report id.


###### 5. /get_report/{reportId}
* Used to fetch report through given report id.
* If report is not available yet, returns status "Running".
* If report is generated, returns that.
* GET request




