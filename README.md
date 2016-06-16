# moproxy
远程反向代理
1.介绍：
moproxy即moddns。
moddns是一款类似花生壳动态域名服务的工具，不同的是本软件采用的是远程反向代理技术，使用它，您可以轻松的让运行在您本地电脑的web程序通过外网访问。
不同于花生壳，本工具不要路由器主机映射，不要端口转发，不限制路由级数，不管是在家，在公司，还是在咖啡馆，只要您的电脑能上网，那么您就可以使用该产品！

关于软件的更新和更多使用信息,请随时关注 http://blog.mocoder.com/moddns 。

2.特别说明：

本软件目前处于开发测试阶段，所以服务器可能有一定程度的不稳定（只是可能哦）,需要建长久网站的盆友还有另谋高就吧。。。另外由于资源有限，在此期间，作者有权随时收回您的使用权力（特别是作者看你不顺眼的时候），还请谅解。

3.使用方法：

注意：本程序是java所写，运行前请保证java运行环境正确安装，并且正确设置了环境变量。
客户端提供控制台和图形界面两种运行方式，在解压压缩包后，您可以看到 config.properties moddns_client.jar等文件。
如果您是window用户，直接双击start-gui.bat 即可图形界面运行程序（推荐）。下面是各运行脚本说明：
start-gui.bat 以图形界面方式启动软件（windows）
start-console.bat 以控制台方式启动软件 （windows）
start-console.sh 以图形界面方式启动软件 (linux)
start-gui.sh 以控制台方式启动软件 （linux）

bin目录下 除了config.properties.sample(示例配置文件),默认是没有配置文件的，等一次通过图形界面正确登录会生成，并保存以后的修改。
注意：如果用控制台登录，登录前请保证有正确的配置文件，您可以根据示例文件修改，也可以先用图形界面生成

4. 目录以及文件介绍

start-gui.bat 以图形界面方式启动软件（windows）
start-console.bat 以控制台方式启动软件 （windows）
start-console.sh 以图形界面方式启动软件 (linux)
start-gui.sh 以控制台方式启动软件 （linux）

moddns_client.jar

客户端主程序，您可以通过以下命令来运行它（推荐直接用作者提供的已经写好的脚本来运行）
控制台运行：java -jar moddns_client.jar -console
图形界面运行：javaw -jar moddns_client.jar -gui
就是这么简单！
另外，请不要在没有装桌面环境的linux下运行图形界面程序。

config.properties

此文件是客户端配置文件，比较重要，在不清楚其含义的情况下不要随意修改配置参数。
下面针对每项进行介绍：

远程ddns服务器主机地址，这个参数一般不需要改变，由作者分配

remote_server_domain=ddns.mocoder.com

远程ddns服务器主机端口，这个参数一般不需要改变，由作者分配
remote_server_port=3333

本地服务器的访问超时时间，以毫秒为单位，默认为15秒，一般不需要修改。如果浏览器请求超过这个时间，会放弃接受本地服务器的返回信息，并通知浏览器超时
request_timeout=15000

如果超过以下设定的时间没有人通过外网访问您的项目，客户端会自动退出，默认是600秒，即10分钟。当然，如果您设置足够大话，moddns服务器端也会主动断开连接

idle_before_exit=600000

客户端连接出现错误达到以下次数时将自动退出

error_count_before_exit=3

缓冲区和传输块大小，这个设置比较关键，不可修改。

buffer_size=2048

日志文件输出日志等级，有效选项为 debug、info、warn、error

log_level=info

日志文件路径：可为相对或者绝对路径，相对路径当前路径为当前classpath

log_path=./client.log


一下信息需要您手动修改

您的账号Id，具体联系作者或管理员

user_id=test

你的密码，这个不用多说

password=1234
分配给你的n级域名，比如申请的时候给您的是test.ddns.mocoder.com ,而服务器域名是 ddns.mocoder.com,则这里写test。

personal_domain=test

您本地服务器的主机名，如localhost、127.0.0.1等等，当然，当前没有限制必须是本机

local_server_host=localhost

您的本地服务器的端口

local_server_port=8080


client.log


日志输出文件，无特殊情况，你可以把config.properties内的log_level设置为info，以避免过多的日志输出占用系统资源。另外您使用时遇到不明原因的错误可以向作者提交此日志文件以帮助作者分析。

4. 注意事项:

1. 如果您是在控制台下运行本程序，请保证配置文件的信息正确（包括用户名、密码和域名信息）,因为一旦运行就会使用配置文件登录。
2.如果是图形界面访问运行，通过图形界面修改了配置信息，则会重新生成配置文件。但是须保证第一次运行时，配置文件除必须手动修改的信息外的所有参数正确。
