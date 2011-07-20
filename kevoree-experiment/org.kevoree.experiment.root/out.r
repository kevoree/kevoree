nodeNames <- c("parapluie21rennesgrid5000fr0","parapluie21rennesgrid5000fr1","parapluie21rennesgrid5000fr2","parapluie21rennesgrid5000fr3","parapluie23rennesgrid5000fr0")
propDelais <- c(0,67,77,90,91)
library(Hmisc)
bpplot(propDelais,main="Downtime propagation delay")