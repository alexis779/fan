require 'rubygems'
require 'em-http'

require 'test/unit'
require 'test/unit/ui/console/testrunner'

# Test a HTTP Head request that follows redirects
class EventMachineHttpTest < Test::Unit::TestCase
	def test_head
		EventMachine.run {
			head
		}
	end

	private
		
	def head
		# URL that does not return a 200 right away
		url = "http://www.dailymotion.com/"

                request = EventMachine::HttpRequest.new(url).head :redirects => 1

                request.callback {
                        status = request.response_header.status
			assert(status == 200, "Did not follow redirects.")
                        EventMachine.stop
                }
	end
end

Test::Unit::UI::Console::TestRunner.run(EventMachineHttpTest)
