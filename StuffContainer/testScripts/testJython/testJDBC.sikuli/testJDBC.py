# the jars must be either in Lib or Extensions
# or you have to use absolute paths
load("ojdbc7.jar")
load("zxJDBC.jar") # bundled in Lib
from com.ziclix.python.sql import zxJDBC
OracleDriver = "oracle.jdbc.driver.OracleDriver"
jdbc_url = "jdbc:oracle:thin:@122.24.93.31:1521:MSCDB"
username = "msc"
password = "msc"
db = zxJDBC.connect(jdbc_url, username, password, OracleDriver)

