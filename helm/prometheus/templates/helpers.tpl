{{- define "prometheus.name" -}}
prometheus
{{- end -}}

{{- define "prometheus.fullname" -}}
{{ include "prometheus.name" . }}
{{- end -}}
