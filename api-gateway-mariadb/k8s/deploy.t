apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${IMAGE_NAME}-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${IMAGE_NAME}
  template:
    metadata:
      annotations:
        prometheus.io/scrape: 'true'
        prometheus.io/port: '8081'
        prometheus.io/path: '/actuator/prometheus'
        update: ${HASHCODE}
      labels:
        app: ${IMAGE_NAME}
    spec:
      imagePullSecrets:
      - name: harbor-registry-secret
      containers:
      - name: ${IMAGE_NAME}
        image: ${DOCKER_REGISTRY}/${IMAGE_NAME}:${VERSION}
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
        env:
        - name: DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: datasource-secrets
              key: database-url
        - name: DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: datasource-secrets
              key: database-username
        - name: DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: datasource-secrets
              key: database-password

