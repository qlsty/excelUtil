# 网络IO
## 1、网络IO方式
*所有IO方式和具体语言无关，由操作系统和硬件支持*
### 同步方式
- blocking IO
- nonblocking IO
- IO multiplexing
- signal driven IO

### 异步方式
- asynchronous IO

## 2、一切还要从网卡说起
*不需要cpu参与*
***接受数据***
网卡接收到数据（来自于网线或者wifi等），如果数据中mac地址与自身mac地址相同（不同则丢弃(网卡工作在混杂模式下不丢弃)），就将数据暂存于网卡自********身FIFO队列，紧接着将数据写入内核缓冲区。然后向CPU发出中断（ROK 就是read ok 标识有数据可读），cpu执行网卡注册在系统的驱动程序处理数据。
***发送数据***
用户进程将调用系统调用将数据写入内核缓冲区，内核进程将数据写入网卡FIFO队列，网卡将FIFO的数据发送出去（通过NIC传送单元），然后向cpu发送中断（TOK -> transmit ok）发送完成

## 3、更上一层Socket
了解完网卡工作方式后，那么用户程序如何读写网卡上的数据呢？linux 将网卡的读写抽象成了一个对文件的读写。这个特殊的文件就是**socket**。
一个完整的C语言socket应用如下
服务端
    #include<stdio.h>
    #include<sys/socket.h>
    #include<netinet/in.h>
    #include<stdlib.h>
    #include<arpa/inet.h>
    #include<unistd.h>
    #include<string.h>
    
    int main(){
    	//创建套接字
    	int serv_sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    
    	//初始化socket元素
    	struct sockaddr_in serv_addr;
    	memset(&serv_addr, 0, sizeof(serv_addr));
    	serv_addr.sin_family = AF_INET;
    	serv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    	serv_addr.sin_port = htons(1234);
    
    	//绑定文件描述符和服务器的ip和端口号
    	bind(serv_sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr));
    
    	//进入监听状态，等待用户发起请求
    	listen(serv_sock, 20);
    	//接受客户端请求
    	//定义客户端的套接字，这里返回一个新的套接字，后面通信时，就用这个clnt_sock进行通信
    	struct sockaddr_in clnt_addr;
    	socklen_t clnt_addr_size = sizeof(clnt_addr);
    	int clnt_sock = accept(serv_sock, (struct sockaddr*)&clnt_addr, &clnt_addr_size);
    
    	//接收客户端数据，并相应
    	char str[256];
    	read(clnt_sock, str, sizeof(str));
    	printf("client send: %s\n",str);
    	strcat(str, "+ACK");
    	write(clnt_sock, str, sizeof(str));
    
    	//关闭套接字
    	close(clnt_sock);
    	close(serv_sock);
    
    	return 0;
    }
客户端
    #include<stdio.h>
    #include<string.h>
    #include<stdlib.h>
    #include<unistd.h>
    #include<arpa/inet.h>
    #include<sys/socket.h>
    
    int main(){
    	//创建套接字
    	int sock = socket(AF_INET, SOCK_STREAM, 0);
    
    	//服务器的ip为本地，端口号1234
    	struct sockaddr_in serv_addr;
    	memset(&serv_addr, 0, sizeof(serv_addr));
    	serv_addr.sin_family = AF_INET;
    	serv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    	serv_addr.sin_port = htons(1234);
    	
    	//向服务器发送连接请求
    	connect(sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr));
    	//发送并接收数据
    	char buffer[40];
    	printf("Please write:");
    	scanf("%s", buffer);
    	write(sock, buffer, sizeof(buffer));
    	read(sock, buffer, sizeof(buffer) - 1);
    	printf("Serve send: %s\n", buffer);
    
    	//断开连接
    	close(sock);
    
    	return 0;
    }
服务端调用accept是 就会阻塞直到有请求进来。一旦有请求进来**clnt_sock**就代表了请求方的文件描述符，可以使用此描述符和请求方通信。主要是 write和read方式。此时调用write如果内核缓冲区已满，则`等待`（blocking IO）直到内核缓冲区有足够空间写入。写入后write返回。调用read方法如果缓冲区没有可读的数据则`一直等待`（blocking IO）到缓冲区有数据可读。将数据从内存缓中区读出。
上面这种blocking IO方式浪费cpu资源因此nonblocking IO应用而生 代码如下
```
      int flags = fcntl(clnt_sock , F_GETFL, 0);
      fcntl(clnt_sock , F_SETFL, flags|O_NONBLOCK);
```
将上述两行代码添加到accept函数后。可以将clnt_sock 设置为非阻塞。底层则调用write和read方法时如果内核缓冲区未准备好 ，则直接返回错误。此时用户进程可根据返回值来做不同操作。表现为write和read不阻塞（blocking IO）。

**IO multiplexing**（netty nginx redis kafka均采用此方式）
随着互联网发展上述两种IO方式在大量客户端同时请求下变得力不从心。主要原因是随着客户端增多维护客户端的socket变得越来困难。一个线程无法同时服务所有客户端。就诞生了一个客户端进来就新起一个线程（tomcat等早期就是如此 单线程服务器向多线程服务器迈进），随着客户量在增大C10K（一个服务器能新建的线程数量是有限的）问题日益突出。此时**select/poll/epoll/kqueue**等技术应运而生。linux 支持select/poll/epoll。略去select/poll细节不表，重点解释epoll。
select/poll 设计以及缺陷 ，其代码如下

































