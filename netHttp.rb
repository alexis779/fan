require 'uri'
require 'net/http'
require 'logger'

class NetHttp

	def initialize
		@logger = Logger.new($stderr)
		@logger.level = Logger::DEBUG

		@httpHash = {}
	end

	def get(url)
		uri = URI.parse(url)

		path = uri.path
		unless uri.query.nil?
			path << "?" + uri.query
		end
		# TODO: check if path is empty

		method = "HEAD"
		puts httpRequest(uri.host, uri.port, uri.path, method)
	end

	def httpRequest(host, port, path, method)
		if method == "HEAD"
			request = Net::HTTP::Head.new(path)
		else
			# TODO: implement other methods
		end
		# TODO: add headers
		#request.initialize_http_header(headers)
		@logger.debug("Getting connection on #{host}:#{port}")
		http = getHttp(host, port)
		@logger.debug("#{method} #{path}")
		response = http.request(request)

		statusCode = response.code
		@logger.debug("#{statusCode} #{response.message}")
		if statusCode == "302"
			location = response["Location"]
			statusCode = httpRequest(host, port, location, method)
		end

		statusCode
	end

	def getHttp(host, port)
		key = host + "_" + port.to_s
		# check if the pool contains corresponding connection
		http = @httpHash[key]
		if http.nil?
			http = Net::HTTP.new(host, port)
			http.set_debug_output $stderr
			# change default timeout of 60 sec
			http.read_timeout = @maxTime
			@httpHash[key] = http
		end

		http
	end
end

http = NetHttp.new
http.get("http://xkings.net/video/25523/polvo-amateur-electra-culo-gordo/")
