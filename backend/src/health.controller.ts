import { Controller, Get } from '@nestjs/common';
import { ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';

@ApiTags('Health')
@Controller('health')
export class HealthController {
  @Get()
  @ApiOperation({ summary: 'Health check' })
  @ApiOkResponse({ schema: { example: { status: 'ok', service: 'unlock-counter-api' } } })
  check() {
    return { status: 'ok', service: 'unlock-counter-api' };
  }
}
