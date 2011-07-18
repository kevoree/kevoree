library(Hmisc)
pdf("/Users/ffouquet/downtimeBPPLOT.pdf")
bpplot(downtime,main="Downtime Percentile repartition")
dev.off()