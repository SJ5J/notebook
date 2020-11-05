1、系统默认分配给kcs的端口是 `CA2h`，使用I/O空间，对应AST2400的kcs通道3.
`The default system base address for an I/O mapped KCS SMS Interface is CA2h.`

2、kcs的寄存器是在BMC里实现的，一共4个寄存器，每个寄存器1个字节。系统侧通过I/O访问这四个寄存器来达到操作kcs的目的。
![kcs](H:\BMC\driver\kcs)
3、kcs状态寄存器的IBF和OBF是由硬件设置的，S1、S0是由软件设置的。
当系统侧向BMC发送kcs请求时，IBF会被置1，触发BMC的kcs中断，BMC在中断里读取状态寄存器，判断现在的状态然后设置S1和S0。

4、kcs的接收和发送全是通过中断完成的。系统端向BMC写数据称为 kcs写。BMC向系统端写数据称为kcs读。