require 'uri'
require 'rubygems'
require 'curb'
require 'logger'

class CurbHttp

	def initialize
		@logger = Logger.new($stderr)
		@logger.level = Logger::DEBUG

		@httpHash = {}
	end

	def get(url)
		method = "HEAD"
		puts httpRequest(url, method)
	end

	def httpRequest(url, method)
		uri = URI.parse(url)
		host = uri.host
		port = uri.port
		path = uri.path
		unless uri.query.nil?
			path << "?" + uri.query
		end

		unless method == "HEAD"
			# TODO: implement other methods
			return nil
		end
		@logger.debug("Getting connection on #{host}:#{port}")

		curl = Curl::Easy.new(url) { |c|
			c.head = true
			c.follow_location = true
		}

		@logger.debug("#{method} #{path}")
		curl.perform

		statusCode = curl.response_code
		@logger.debug(statusCode)

		statusCode
	end

=begin
	def getHttp(host, port)
		key = host + "_" + port.to_s
		# check if the pool contains corresponding connection
		http = @httpHash[key]
		if http.nil?

			@httpHash[key] = http
		end

		http
	end
=end
end

http = CurbHttp.new
http.get("http://www.dailymotion.com/")
