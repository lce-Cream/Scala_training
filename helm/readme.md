# Setup
Start Docker daemon, start Minikube.


Test that everything is ok.
```bash
helm install test-release ./ --dry-run --debug
```

Install the chart release.
```bash
helm install test-release ./
```

Check currently installed releases.
```bash
helm list --all
```

Delete the release.
```bash
helm del test-release
```

You can also package this chart for easier distrubution.
```bash
helm package test-release-pack
```
