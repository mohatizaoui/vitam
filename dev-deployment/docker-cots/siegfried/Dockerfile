FROM golang:1.21.5-alpine3.19
ARG SIEGFRIED_VERSION=latest
RUN go install github.com/richardlehane/siegfried/cmd/sf@${SIEGFRIED_VERSION}
RUN sf -update
ENV SIEGFRIED_HOST=0.0.0.0
ENV SIEGFRIED_PORT=19000
EXPOSE 19000
CMD ["/bin/sh", "-c", "sf -serve ${SIEGFRIED_HOST}:${SIEGFRIED_PORT} -log e,w,u,o,p,t"]
